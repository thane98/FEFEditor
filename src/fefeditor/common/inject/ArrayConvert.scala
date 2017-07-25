package fefeditor.common.inject

import java.nio.{ByteBuffer, ByteOrder}

/**
  * Created by Ethan on 3/22/2017.
  */
object ArrayConvert {
  def toInteger(bytes: Array[Byte]): Int = {
    val buffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
    buffer.put(bytes)
    buffer.getInt(0)
  }

  def toByteArray(a: Int): Array[Byte] = {
    val bb = ByteBuffer.allocate(4)
    bb.order(ByteOrder.LITTLE_ENDIAN)
    bb.putInt(a)
    bb.array
  }
}
