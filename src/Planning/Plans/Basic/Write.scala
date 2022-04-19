package Planning.Plans.Basic

import Planning.Plan
import Utilities.Property

class Write[T](property: Property[T], lambda: () => T) extends Plan{
  
  override def onUpdate() {
    property.set(lambda())
  }
}
