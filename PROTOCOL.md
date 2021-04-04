# Core Protocol



### Head 

server her zaman "Upload-Offset" headerini koymak zorundadir. 0 olsa bile koymali. Eger server bir sekilde 
dosyanin tum boyutunu biliyorsa "Upload-Length" headerini koymalidir.

Eger ilgili resource serverda bulunmuyorsa "Upload-Offset" koymadan 404 yada 410 status koduyla client a donmelidir.

Server "Cache-Control: no-store" headerini response a koymalidir. Browser cache icin sanirim



### Patch

Server a gelen requestteki "Upload-Offset" valuesu resourcedaki size ile ayni olmak zorundadir eger degilse
409 conflict donmelidir.

Tum patch requestleri content Type application/offset-octet-stream olmalidir , olmadigi takdirde 415 donmelidir.


```
The Server MUST acknowledge successful PATCH requests with the 204 No Content status. 
It MUST include the Upload-Offset header containing the new offset. 
The new offset MUST be the sum of the offset before the PATCH request and the number 
of bytes received and processed or stored during the current PATCH request.


If the server receives a PATCH request against a non-existent resource it SHOULD return a 404 Not Found status.

Both Client and Server, SHOULD attempt to detect and handle network errors predictably. 
They MAY do so by checking for read/write socket errors, as well as setting read/write timeouts.
A timeout SHOULD be handled by closing the underlying connection.
The Server SHOULD always attempt to store as much of the received data as possible.

```



### Post


