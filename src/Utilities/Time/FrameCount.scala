package Utilities.Time

abstract class FrameCount(frames: Int) {

  def apply(): Int = frames
  final def totalSeconds: Int = frames / 24
  final def seconds: Int = totalSeconds % 60
  final def minutes: Int = totalSeconds / 60

  final def +(other: FrameCount)  : FrameCount = Frames(this() + other())
  final def -(other: FrameCount)  : FrameCount = Frames(this() - other())
  final def +(other: Int)         : FrameCount = Frames(this() + other)
  final def -(other: Int)         : FrameCount = Frames(this() - other)

  final override def toString: String = minutes + ":" + "%02d".format(seconds)
}

