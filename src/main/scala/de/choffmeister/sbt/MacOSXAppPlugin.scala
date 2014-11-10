package de.choffmeister.sbt

import sbt._
import sbt.Keys._
import de.choffmeister.sbt.JarsPlugin._

object MacOSXAppPlugin extends Plugin {
  val macosxAppName = SettingKey[String]("macosxAppName")
  val macosxAppTarget = SettingKey[File]("macosxAppTarget")
  val macosxAppMainClass = SettingKey[String]("macosxAppMainClass")
  val macosxAppHighResolution = SettingKey[Boolean]("macosxAppHighResolution")
  val macosxAppJavaJVMOptions = SettingKey[Seq[String]]("macosxAppJavaJVMOptions")
  val macosxAppIcon = SettingKey[Option[File]]("macosxAppIcon")

  val macosxAppJavaJars = TaskKey[Seq[File]]("macosxAppJavaJars")
  val macosxAppPackage = TaskKey[File]("macosxAppPackage")

  lazy val macosxAppSettings = jarsSettings ++ Seq[Def.Setting[_]](
    macosxAppName := name.value,
    macosxAppTarget := target.value / (macosxAppName.value + ".app"),
    macosxAppHighResolution := true,
    macosxAppJavaJVMOptions := Seq.empty,
    macosxAppIcon := None,
    macosxAppJavaJars := jarsAll.value.map(_._1),
    macosxAppPackage := {
      val appTarget = macosxAppTarget.value
      val contentTarget = appTarget / "Contents"
      val resourceTarget = contentTarget / "Resources"
      val macosTarget = contentTarget / "MacOS"
      val javaTarget = contentTarget / "Java"
      val launcherTarget = macosTarget / "launcher"
      val iconTarget = resourceTarget / "icon.icns"
      val jars = macosxAppJavaJars.value

      // clean and recreate .app folder
      IO.delete(appTarget)
      appTarget.mkdirs()
      contentTarget.mkdir()
      resourceTarget.mkdir()
      macosTarget.mkdir()
      javaTarget.mkdir()

      // write Info.plist file
      val info = PList(
        "CFBundleInfoDictionaryVersion" -> PListString("6.0"),
        "CFBundleExecutable" -> PListString(IO.relativize(macosTarget, launcherTarget).get),
        "CFBundleIconFile" -> PListString(IO.relativize(resourceTarget, iconTarget).get),
        "CFBundleName" -> PListString(macosxAppName.value),
        "CFBundleDisplayName" -> PListString(macosxAppName.value),
        "CFBundlePackageType" -> PListString("APPL"),
        "CFBundleIdentifier" -> PListString(organization.value + "." + name.value),
        "CFBundleVersion" -> PListString(version.value),
        "CFBundleShortVersionString" -> PListString(version.value),
        "CFBundleSignature" -> PListString("????"),
        "NSHighResolutionCapable" -> PListString(macosxAppHighResolution.value.toString),
        "JVMMainClassName" -> PListString(macosxAppMainClass.value),
        "JVMOptions" -> PListArray(macosxAppJavaJVMOptions.value.map(PListString).toList)
      )
      PList.writeToFile(info, contentTarget / "Info.plist")

      // add package info
      IO.write(contentTarget / "PkgInfo", "APPL????")

      // add executable
      IO.write(launcherTarget, IO.readBytes(getClass.getResourceAsStream("/JavaAppLauncher")))
      launcherTarget.setExecutable(true, false)

      // add icon
      macosxAppIcon.value match {
        case Some(icon) => IO.copyFile(icon, iconTarget)
        case _ => IO.write(iconTarget, IO.readBytes(getClass.getResourceAsStream("/GenericApp.icns")))
      }

      // add JAR files
      jars.foreach(jar => IO.copyFile(jar, javaTarget / jar.getName))

      appTarget
    }
  )
}
