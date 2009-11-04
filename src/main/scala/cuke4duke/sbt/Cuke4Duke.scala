package cuke4duke.sbt

import _root_.sbt._

trait Cuke4Duke extends BasicManagedProject with ScalaPaths {
  def cuke4DukeVersion = "0.1.8"
  def cuke4DukeGems = List("cucumber --version 0.4.3 --source http://gems.rubyforge.org/")
  def cuke4DukeJvmArgs = List("-Dcuke4duke.objectFactory=cuke4duke.internal.jvmclass.PicoFactory")
  def jRubyHome = info.projectPath / "lib_managed" / "cuke4duke_gems"
  def featuresDirectory = info.projectPath / "features"
  def extraCucumberOptions: List[String] = Nil

  val cuke4DukeRepo = "Cuke4Duke Maven Repository" at "http://cukes.info/maven"
  val cuke4DukeDependency = "cuke4duke" % "cuke4duke" % cuke4DukeVersion % "test"
  val picoContainerDependency = "org.picocontainer" % "picocontainer" % "2.8.3" % "test"

  val gemPath = jRubyHome / "gems"
  val cucumberBin = gemPath / "bin" / "cucumber"

  private val jruby = new JRuby(fullClasspath(Configurations.Test), cuke4DukeJvmArgs, jRubyHome, gemPath, log)

  def installCuke4DukeGems() = {
    log.info("Installing Cuke4Duke gems...")
    cuke4DukeGems.map(jruby.installGem(_)).reduceLeft(_ + _)
  }

  lazy val updateNoInstallCucumberGem = super.updateAction
  lazy val updateCuke4DukeGems = task {
    installCuke4DukeGems match {
      case 0 => None
      case _ => Some("Installing Cuke4Duke gems failed!")
    }
  }
  override def updateAction = updateCuke4DukeGems dependsOn(updateNoInstallCucumberGem)

  def runCucumberFeatures() = {
    jruby(List("-r", "cuke4duke",
               cucumberBin.absolutePath,
               featuresDirectory.absolutePath,
               "--require", testCompilePath.absolutePath,
               "--color") ++ extraCucumberOptions)
  }

  lazy val features = task {
    runCucumberFeatures match {
      case 0 => None
      case i => Some("Cucumber features failed! - Exit Code: " + i)
    }
  }
}