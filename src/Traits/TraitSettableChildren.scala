package Traits

import Plans.Plan

trait TraitSettableChildren {
  
  var _children:Iterable[Plan] = List.empty
  
  def getChildren:Iterable[Plan] = {
    _children
  }
  
  def setChildren(kids:Iterable[Plan]) {
    _children = kids
  }
}
