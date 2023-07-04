package Information.Battles.Prediction.Simulation

import Mathematics.Points.Pixel

abstract class SimulationEvent(sim: Simulacrum) {
  protected def describe(sim: Option[Simulacrum]): String = sim.map(_.describe).getOrElse("(Nobody)")
  protected def describePixel(pixel: Option[Pixel]): String = pixel.map(_.toString).getOrElse("[Nowhere]")

  val frame     : Int                 = sim.simulation.battle.simulationFrames
  val from      : Pixel               = sim.pixel
  val target    : Option[Simulacrum]  = sim.target
  val targetAt  : Option[Pixel]       = target.map(_.pixel)

  def to: Pixel
  def draw() {}
}
