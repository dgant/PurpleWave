package Information.Fingerprinting.Generic

case class GameTime(minutes: Int, seconds: Int) {
 
  def this(totalFrames: Int) = this(
    (totalFrames / 24) / 60,
    (totalFrames / 24) % 60)
 
  implicit def frames: Int = 24 * (60 * minutes + seconds)
  
  def apply(): Int = frames
  override def toString: String = minutes + ":" + "%02d".format(seconds)
}
