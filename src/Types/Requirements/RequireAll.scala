package Types.Requirements

import Types.Traits.RequiresInitialization

class RequireAll(requirements:Requirement*)
  extends Requirement with RequiresInitialization {
  
  val _requirements = requirements.toList
  
  override def fulfill() {
    _requireInitialization()
    for(requirement <- _requirements) {
      requirement.fulfill()
      
      if ( ! requirement.isFulfilled) {
        abort()
        return
      }
    }
    
    isFulfilled = true
  }
  
  override def abort() {
    _requireInitialization()
    _requirements.foreach(_.abort())
  }
  
  override def _onInitialization() {
    _requirements.foreach(_.buyer = buyer)
  }
}
