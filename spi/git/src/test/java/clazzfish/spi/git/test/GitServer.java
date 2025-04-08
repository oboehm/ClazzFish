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

    public static void main(String[] args) throws Exception {
        Server server = startServer();

        // finally wait for the Server being stopped
        server.join();
    }

    public static Server startServer() throws Exception {
        Repository repository = createNewRepository();

        populateRepository(repository);

        // Create the JGit Servlet which handles the Git protocol
        GitServlet gs = new GitServlet();
        gs.setRepositoryResolver((req, name) -> {
            repository.incrementOpen();
            return repository;
        });

        // start up the Servlet and start serving requests
        return configureAndStartHttpServer(gs);
    }

    private static Server configureAndStartHttpServer(GitServlet gs) throws Exception {
        Server server = new Server(8080);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        ServletHolder holder = new ServletHolder(gs);

        handler.addServletWithMapping(holder, "/*");

        server.start();
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
