package Placement.Access

import Debugging.SimpleString

object PlaceLabels {
  trait PlaceLabel extends SimpleString
  object TownHall         extends PlaceLabel
  object Gas              extends PlaceLabel
  object GroundProduction extends PlaceLabel
  object Tech             extends PlaceLabel
  object Supply           extends PlaceLabel
  object Defensive        extends PlaceLabel
  object DefendChoke      extends PlaceLabel
  object DefendWorkers    extends PlaceLabel
  object Wall             extends PlaceLabel
  object PriorityPower    extends PlaceLabel
}
