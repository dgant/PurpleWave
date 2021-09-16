package Utilities.Time

abstract class FrameCount(frames: Int) {

  def apply(): Int = frames
  def totalSeconds: Int = frames / 24
  def seconds: Int = totalSeconds % 60
  def minutes: Int = totalSeconds / 60

  def +(other: FrameCount): FrameCount = Frames(this() + other())
  def -(other: FrameCount): FrameCount = Frames(this() - other())

  override def toString: String = minutes + ":" + "%02d".format(seconds)
}

