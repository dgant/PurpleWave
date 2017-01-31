package Types.Traits

trait RequiresInitialization {
  
  var _isInitialized:Boolean = false
  
  def _requireInitialization() {
    if ( ! _isInitialized) {
      _onInitialization()
    }
    _isInitialized = true
  }
  
  def _onInitialization()
}
