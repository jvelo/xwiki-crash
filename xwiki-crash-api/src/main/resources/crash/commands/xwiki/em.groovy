import java.util.Collection;

import org.crsh.command.CRaSHCommand
import org.crsh.cmdline.annotations.Usage
import org.crsh.cmdline.annotations.Command
import org.crsh.cmdline.annotations.Argument
import org.crsh.cmdline.annotations.Required
import org.crsh.shell.ui.UIBuilder

import org.xwiki.extension.CoreExtension;

@Usage("XWiki extension manager commands")
class em extends CRaSHCommand {

  @Usage("List extensions")
  @Command
  void list() {
      def extensionRepository = 
             com.xpn.xwiki.web.Utils.getComponent(org.xwiki.extension.repository.CoreExtensionRepository.class)
             
      Collection<CoreExtension> extensions = extensionRepository.getCoreExtensions();
      extensions.each { extension ->
        out << "${extension.id} (${extension.name})\n"
      }
  }

}