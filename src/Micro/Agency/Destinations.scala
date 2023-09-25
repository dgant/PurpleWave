package Micro.Agency

object Destinations {
  // Retreat points must have negative values
  val Home    : Int = -2  // Ultimate retreat point;      a base
  val Redoubt : Int = -1  // Intermediate retreat point;  a disengage formation slot
  val Terminus: Int = 0   // Final destination
  val Perch   : Int = 1   // Position to launch attack
  val Station : Int = 2   // Intermediate destination;    an engage formation slot
  val Forced  : Int = 3   // Chosen by applying forces
  val Decision: Int = 4   // A final decision on what to do

  val All: Vector[Int] = Vector(
    Home,
    Redoubt,
    Terminus,
    Perch,
    Station,
    Decision).sorted
}
