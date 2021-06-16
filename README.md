# tusd

<img alt="Tus logo" src="https://github.com/tus/tus.io/blob/master/assets/img/tus1.png?raw=true" width="30%" align="right" />

> **tus** is a protocol based on HTTP for *resumable file uploads*. Resumable
> means that an upload can be interrupted at any moment and can be resumed without
> re-uploading the previous data again. An interruption may happen willingly, if
> the user wants to pause, or by accident in case of an network issue or server
> outage.

tusd is the official reference implementation of
the [tus resumable upload protocol](http://www.tus.io/protocols/resumable-upload.html). The protocol specifies a
flexible method to upload files to remote servers using HTTP. The special feature is the ability to pause and resume
uploads at any moment allowing to continue seamlessly after e.g. network interruptions.

**Protocol version:** 1.0.0

### Protocol Extensions Support

- Creation [ x ]
- Creation With Upload [ x ]
- Expiration [ x ]
- Termination [ x ]
- Concatenation [ x ]
- Checksum [  ]

# Stores

- FileStore [ x ] , supports extension
  creation,creation-with-upload,creation-defer-length,expiration,concatenation,termination
- S3Store [ x ] , supports extension
  creation,creation-with-upload,creation-defer-length,expiration,concatenation,termination
- GCSStore [ ]

s3 store uses file disk to supports some extensions s3 store not using multipart todo

## About Implementation Of This Server

- Supports Chunk upload 
- Supports Parallel upload
- Zero memory allocation 

# Not Production Ready !!

Kind of ready for file storage :)
