package Debugging.Visualizations

object Forces {
  lazy val travel     : ForceLabel = ForceLabel("Traveling",  Colors.NeonBlue)
  lazy val leaving    : ForceLabel = ForceLabel("Leaving",    Colors.NeonOrange)
  lazy val threat     : ForceLabel = ForceLabel("Threat",     Colors.NeonRed)
  lazy val sneaking   : ForceLabel = ForceLabel("Sneaking",   Colors.NeonYellow)
  lazy val target     : ForceLabel = ForceLabel("Target",     Colors.NeonGreen)
  lazy val spacing    : ForceLabel = ForceLabel("Spacing",    Colors.NeonTeal)
  lazy val regrouping: ForceLabel = ForceLabel("Regrouping", Colors.NeonIndigo)
  lazy val pushing   : ForceLabel = ForceLabel("Spreading",  Colors.NeonViolet)
  lazy val sum       : ForceLabel = ForceLabel("Sum",        Colors.White)
}
