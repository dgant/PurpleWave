package Lifecycle

class BooleanFlag(flagname: String) {
  val flagEnabled = new FileFlag(flagname + ".enabled")
  val flagDisabled = new FileFlag(flagname + ".disabled")

  private lazy val enabled: Boolean = {
    val isEnabled = flagEnabled()
    val isDisabled = flagDisabled()

    if ( ! (isEnabled || isDisabled)) {
      With.logger.warn("Missing boolean file flag " + flagname)
    }

    isEnabled && ! isDisabled
  }


  def apply(): Boolean = enabled

  override def toString: String = "Flag: " + flagname + (if (enabled) "(Enabled)" else "(Disabled)")
}
