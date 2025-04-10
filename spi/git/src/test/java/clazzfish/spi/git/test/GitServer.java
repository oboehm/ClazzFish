/*
 * Copyright (c) 2025 by Oli B.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express orimplied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * (c)reated 07.04.25 by oboehm
 */
package clazzfish.spi.git.test;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

/**
 * This is a very simple Git server which was copied and adapted from
 * <a href="https://github.com/centic9/jgit-cookbook/tree/master">jgit-cookboo</a>.
 * It is used for testing only. Currently it will return the same repository
 * for any name that is requested, there is no logic to distinguish between
 * different repos in this simple example.
 * <p>
 * After starting this application, you can use something like
 *      git clone http://localhost:8080/TestRepo
 * to clone the repository from the running server.
 * </p>
 *
 * @author oboehm
 * @since 2.6 (07.04.25)
 */
public class GitServer {

    private final Server server;

    public GitServer() throws GitAPIException, IOException {
        this(8080);
    }

    public GitServer(int port) throws GitAPIException, IOException {
        GitServlet gs = createGitServlet();
        server = configureHttpServer(gs, port);
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public static void main(String[] args) throws Exception {
        GitServer server = new GitServer();
        server.start();
        // finally wait for the Server being stopped
        server.join();
    }

    /**
     * Creates the JGit Servlet which handles the Git protocol.
     *
     * @return JGit Servlet
     * @throws IOException     in case of I/O errors
     * @throws GitAPIException in case of problems with Git
     */
    private static GitServlet createGitServlet() throws IOException, GitAPIException {
        Repository repository = createNewRepository();
        populateRepository(repository);
        GitServlet gs = new GitServlet();
        gs.setRepositoryResolver((req, name) -> {
            repository.incrementOpen();
            return repository;
        });
        return gs;
    }

    /**
     * Start up the servlet and start serving requests.
     *
     * @param gs the JGit Servlet
     * @return the started server
     * @throws Exception if server start fails
     */
    private static Server configureAndStartHttpServer(GitServlet gs, int port) throws Exception {
        Server server = configureHttpServer(gs, port);
        server.start();
        return server;
    }

    private static Server configureHttpServer(GitServlet gs, int port) {
        Server server = new Server(port);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        ServletHolder holder = new ServletHolder(gs);
        handler.addServletWithMapping(holder, "/*");
        return server;
    }

    private static void populateRepository(Repository repository) throws IOException, GitAPIException {
        // enable pushing to the sample repository via http
        repository.getConfig().setString("http", null, "receivepack", "true");

        try (Git git = new Git(repository)) {
            File myfile = new File(repository.getDirectory().getParent(), "testfile");
            if(!myfile.createNewFile()) {
                throw new IOException("Could not create file " + myfile);
            }

            git.add().addFilepattern("testfile").call();

            System.out.println("Added file " + myfile + " to repository at " + repository.getDirectory());

            git.commit().setMessage("Test-Checkin").call();
        }
    }

    private static Repository createNewRepository() throws IOException {
        // prepare a new folder
        File localPath = File.createTempFile("TestGitRepository", "");
        if(!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        if(!localPath.mkdirs()) {
            throw new IOException("Could not create directory " + localPath);
        }

        // create the directory
        Repository repository = FileRepositoryBuilder.create(new File(localPath, ".git"));
        repository.create();

        return repository;
    }

}
