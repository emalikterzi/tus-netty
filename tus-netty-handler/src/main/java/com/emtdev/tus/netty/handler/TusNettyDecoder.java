package com.emtdev.tus.netty.handler;

import com.emtdev.tus.netty.event.TusEventPublisher;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.StringUtil;

@ChannelHandler.Sharable
public class TusNettyDecoder extends ChannelInboundHandlerAdapter {

    protected final static String OPTIONS = "OPTIONS";
    protected final static String HEAD = "HEAD";
    protected final static String DELETE = "DELETE";
    protected final static String POST = "POST";
    protected final static String PATCH = "PATCH";

    private final TusOptionsHandler tusOptionsHandler;
    private final TusConfiguration tusConfiguration;
    private final TusHeadHandler tusHeadHandler;
    private final TusDeleteHandler tusDeleteHandler;
    private final TusResponseInterceptorHandler tusDuplexHandler = new TusResponseInterceptorHandler();
    private final TusWrongRequestHandler tusWrongRequestHandler = new TusWrongRequestHandler();
    private final TusEventPublisher eventPublisher;

    public TusNettyDecoder(TusConfiguration tusConfiguration) {

        if (tusConfiguration.getLocationProvider() != null && tusConfiguration.getListeners() != null &&
                !tusConfiguration.getListeners().isEmpty()) {
            eventPublisher = new TusEventPublisher(tusConfiguration.getTusEventExecutorProvider(), tusConfiguration.getListeners(), tusConfiguration.getStore());
        } else {
            eventPublisher = new TusEventPublisher(null, null, null);
        }

        this.tusOptionsHandler = new TusOptionsHandler(tusConfiguration);
        this.tusConfiguration = tusConfiguration;
        this.tusHeadHandler = new TusHeadHandler(tusConfiguration);
        this.tusDeleteHandler = new TusDeleteHandler(tusConfiguration, eventPublisher);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {

        if (message instanceof HttpRequest) {

            HttpRequest msg = (HttpRequest) message;
            HttpRequestAccessor accessor = HttpRequestAccessor.of(msg);

            if (isValidPath(msg.uri())) {

                ctx.pipeline().addLast(tusDuplexHandler);

                String overrideMethod = accessor.httpMethodOverride();

                String requestMethod = msg.method().toString();

                if (!StringUtil.isNullOrEmpty(overrideMethod)) {
                    requestMethod = overrideMethod;
                }

                if (OPTIONS.equals(requestMethod)) {
                    ctx.pipeline().addLast(tusOptionsHandler);
                } else if (HEAD.equals(requestMethod)) {
                    ctx.pipeline().addLast(tusHeadHandler);
                } else if (DELETE.equals(requestMethod)) {
                    ctx.pipeline().addLast(tusDeleteHandler);
                } else if (POST.equals(requestMethod)) {
                    ctx.pipeline().addLast(new TusPostHandler(this.tusConfiguration, eventPublisher));
                } else if (PATCH.equals(requestMethod)) {
                    ctx.pipeline().addLast(new TusPatchHandler(this.tusConfiguration, eventPublisher));
                } else {
                    ctx.pipeline().addLast(tusWrongRequestHandler);
                }
            }

            ctx.fireChannelRead(message);
        } else if (message.equals(LastHttpContent.EMPTY_LAST_CONTENT)) {
            ReferenceCountUtil.release(message);
        } else {
            ctx.fireChannelRead(message);
        }
    }


    private boolean isValidPath(String uri) {
        return true;
    }
}
