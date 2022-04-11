package Placement.Templating

import ProxyBwapi.UnitClasses.UnitClass

abstract class PlaceLabel {
  var classFilter: UnitClass => Boolean = u => true

  def this(classes: UnitClass*) {
    this()
    classFilter = classes.contains
  }
}
