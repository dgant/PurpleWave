package Placement.Access

import Debugging.SimpleString

object PlaceLabels {
  trait PlaceLabel extends SimpleString
  object Important            extends PlaceLabel
  object Unimportant          extends PlaceLabel
  object TownHall             extends PlaceLabel
  object MacroHatch           extends PlaceLabel
  object Gas                  extends PlaceLabel
  object GroundProduction     extends PlaceLabel
  object Tech                 extends PlaceLabel
  object Supply               extends PlaceLabel
  object Defensive            extends PlaceLabel
  object DefendEntrance       extends PlaceLabel
  object DefendHall           extends PlaceLabel
  object DefendAir            extends PlaceLabel
  object DefendGround         extends PlaceLabel
  object Wall                 extends PlaceLabel
  object AnyProxy             extends PlaceLabel
  object ProxyGround          extends PlaceLabel
  object ProxyAir             extends PlaceLabel
}
