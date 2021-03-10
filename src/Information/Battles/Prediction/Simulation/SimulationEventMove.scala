package Information.Battles.Prediction.Simulation

import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.Pixel

case class SimulationEventMove(
  frame     : Int,
  sim       : Simulacrum,
  pixelFrom : Pixel,
  pixelTo   : Pixel,
  frames    : Int)
    extends SimulationEvent {

  override def toString: String = f"$frame: ${describe(sim)} moves ${pixelTo.subtract(pixelFrom)} from $pixelFrom to $pixelTo in $frames frames"

  override def draw() {
    DrawMap.arrow(pixelFrom, pixelTo, sim.realUnit.player.colorMedium)
  }

  override def from: Pixel = pixelFrom
  override def to: Pixel = pixelFrom
}
