package Utilities.Caching

class LimiterFrame(action: () => Unit) extends LimiterBase(action) {
  override protected def frameDelay: Int = 1
}
