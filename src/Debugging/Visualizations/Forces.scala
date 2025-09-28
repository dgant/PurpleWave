package Debugging.Visualizations

import Mathematics.Physics.Force

object Forces {
  lazy val travel     : ForceLabel = ForceLabel("Travel",     Colors.NeonBlue)
  lazy val leaving    : ForceLabel = ForceLabel("Leaving",    Colors.NeonOrange)
  lazy val threat     : ForceLabel = ForceLabel("Threat",     Colors.NeonRed)
  lazy val sneaking   : ForceLabel = ForceLabel("Sneaking",   Colors.NeonYellow)
  lazy val target     : ForceLabel = ForceLabel("Target",     Colors.NeonGreen)
  lazy val spacing    : ForceLabel = ForceLabel("Spacing",    Colors.NeonTeal)
  lazy val regrouping : ForceLabel = ForceLabel("Regrouping", Colors.NeonIndigo)
  lazy val pushing    : ForceLabel = ForceLabel("Pushing",    Colors.NeonViolet)
  lazy val sum        : ForceLabel = ForceLabel("Sum",        Colors.White)

  val up        : Force = Force(  0,  - 1 )
  val down      : Force = Force(  0,    1 )
  val left      : Force = Force(- 1,    0 )
  val right     : Force = Force(  1,    0 )
  val upLeft    : Force = Force(- 1,  - 1 ).normalize
  val upRight   : Force = Force(  1,  - 1 ).normalize
  val downLeft  : Force = Force(- 1,    1 ).normalize
  val downRight : Force = Force(  1,    1 ).normalize
}
