package Planning.Plans.GamePlans.All

trait Modal {
  def isComplete: Boolean
  def update(): Unit
}
