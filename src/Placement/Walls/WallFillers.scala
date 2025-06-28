package Placement.Walls

import Debugging.SimpleString

object WallFillers {
  trait WallFiller      extends SimpleString
  object NoFiller       extends WallFiller
  object PylonsCannons  extends WallFiller
}
