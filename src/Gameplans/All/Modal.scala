package Gameplans.All

trait Modal {
  def isComplete: Boolean
  def update(): Unit
}