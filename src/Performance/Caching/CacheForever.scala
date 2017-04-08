package Performance.Caching

class CacheForever[T](ourCalculator:() => T) {
  
  private var value:Option[T] = None
  
  def get:T = {
    if (value.isEmpty) value = Some(ourCalculator.apply())
    value.get
  }
}

