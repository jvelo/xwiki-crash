package org.xwiki.crash;

import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Helper class, copied over from crash-demo
 * 
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class CommandExecution implements ShellProcessContext {

  /** . */
  final ShellProcess process;

  /** . */
  final CountDownLatch latch = new CountDownLatch(1);

  /** . */
  final AtomicReference<ShellResponse> shellResponse = new AtomicReference<ShellResponse>();

  /** . */
  final int width;

  CommandExecution(ShellProcess process, int width) {
    this.process = process;
    this.width = width;
  }

  ShellResponse execute() {
    process.execute(this);
    try {
      latch.await();
    }
    catch (InterruptedException e) {
      // Should handle this case
      e.printStackTrace();
    }
    return shellResponse.get();
  }

  public int getWidth() {
    return width;
  }
  public String getProperty(String name) {
    return null;
  }
  public String readLine(String msg, boolean echo) {
    // Cannot be implemented at the moment
    return null;
  }
  public void end(ShellResponse response) {
    shellResponse.set(response);
    latch.countDown();
  }

  void cancel() {
    process.cancel();
  }
}
