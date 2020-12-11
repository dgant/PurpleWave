package Utilities

case class GameTime(minutes: Int, seconds: Int) extends AbstractGameTime(minutes, seconds) {
   def this(totalFrames: Int) = this(
    (totalFrames / 24) / 60,
    (totalFrames / 24) % 60)
}
