package com.github.mumoshu.play2.memcached

import java.io.{ObjectOutputStream, ByteArrayOutputStream, ObjectStreamClass}

import net.spy.memcached.transcoders.SerializingTranscoder

class CustomSerializing extends SerializingTranscoder{

  // You should not catch exceptions and return nulls here,
  // because you should cancel the future returned by asyncGet() on any exception.
  override protected def deserialize(data: Array[Byte]): java.lang.Object = {
    new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(data)) {
      override protected def resolveClass(desc: ObjectStreamClass) = {
        Class.forName(desc.getName(), false, play.api.Play.current.classloader)
      }
    }.readObject()
  }

  // We don't catch exceptions here to make it corresponding to `deserialize`.
  override protected def serialize(obj: java.lang.Object) = {
    val bos: ByteArrayOutputStream = new ByteArrayOutputStream()
    // Allows serializing `null`.
    // See https://github.com/mumoshu/play2-memcached/issues/7
    new ObjectOutputStream(bos).writeObject(obj)
    bos.toByteArray()
  }
}
