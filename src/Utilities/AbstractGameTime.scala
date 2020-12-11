package Utilities

abstract class AbstractGameTime(minutes: Int, seconds: Int) {

  implicit def frames: Int = 24 * (60 * minutes + seconds)

  def apply(): Int = frames
  override def toString: String = minutes + ":" + "%02d".format(seconds)
}

