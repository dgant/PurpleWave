package Planning.Plans.GamePlans

trait Modal {
  def isComplete: Boolean
  def update(): Unit
}
