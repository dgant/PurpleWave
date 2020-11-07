package Debugging.Visualizations

import bwapi.Color

case class ForceLabel(name: String, color: Color) {
  override def toString: String = "Force: " + name
}
