package Planning.Plans.Basic

import Planning.{Plan, Property}

class Write[T](property: Property[T], value: T) extends Plan{
  
  override def onUpdate() {
    property.set(value)
  }
}
