import java.util.Collection;

import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Required
import org.crsh.shell.ui.UIBuilder

import org.xwiki.extension.CoreExtension;
import org.xwiki.extension.InstalledExtension;

@Usage("XWiki extension manager commands")
class em extends CRaSHCommand {

  @Usage("List core extensions")
  @Command
  void core() {
      def extensionRepository = 
             com.xpn.xwiki.web.Utils.getComponent(org.xwiki.extension.repository.CoreExtensionRepository.class)
             
      Collection<CoreExtension> extensions = extensionRepository.getCoreExtensions();
      extensions.each { extension ->
        out << "${extension.id} (${extension.name})\n"
      }
  }

  @Usage("List installed extensions")
  @Command
  void installed() {
      def extensionScriptService =
             com.xpn.xwiki.web.Utils.getComponent(org.xwiki.script.service.ScriptService.class, "extension")
             
      Collection<InstalledExtension> extensions = extensionScriptService.getInstalledExtensions();
      extensions.each { extension ->
        out << "${extension.id} (${extension.name})\n"
      }
  }
  
}