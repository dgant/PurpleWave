package Micro.Agency

import Debugging.SimpleString

object Destinations {

  class DestinationLevel(val level: Int) extends SimpleString

  // Retreat points must have negative values

  object Home     extends DestinationLevel(-2)  // Ultimate retreat point;      a base
  object Redoubt  extends DestinationLevel(-1)  // Intermediate retreat point;  a disengage formation slot
  object Terminus extends DestinationLevel(0)   // Final destination
  object Perch    extends DestinationLevel(1)   // Position to launch attack
  object Station  extends DestinationLevel(2)   // Intermediate destination;    an engage formation slot
  object Forced   extends DestinationLevel(3)   // Chosen by applying forces
  object Decision extends DestinationLevel(4)   // A final decision on what to do

  val All: Vector[DestinationLevel] = Vector(
    Home,
    Redoubt,
    Terminus,
    Perch,
    Station,
    Forced,
    Decision)
}
