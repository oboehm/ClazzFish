# Module clazzfish-spi-git

This module supports the GIT protocol to import/export statistics.
It supports URIs like

* https://github.com/oboehm/ClazzFish.git or
* ssh://git@github.com/oboehm/ClazzFish.git

It is recommended to use SSH because HTTPS needs user/password authentification which is not yet supported:

    java -Dclazzfish.dump.uri=ssh://git@github.com/oboehm/ClazzFish.git

This will import and export `ClazzStatistic.csv` to a GitHub repository.


## SSH-Support

SSH works with private and public keys.
To access a GIT repository via SSH you must usually upload your public key to the GIT server.
Your private key must be known by this module otherwise you'll get an "auth failed" when it tries to connect to the git server.
Ususally both are store in your home directory in the .ssh directory:

* id_rsa: private key (RSA algorithm)
* id_rsa.pub: public key


### Define SSH-Key

As default clazzfish-spi-git module uses $HOME/id_rsa as private SSH key.
If you want to use another SSH key you can set the environment variable `clazzfish.git.ssh.keyfile`:

    java -Dclazzfish.git.ssh.keyfile=id_ed25519 ...

This will use `id_ed25519` in the current directory as SSH key.


### Invalid Privatekey

clazzfish-spi-git uses [JSch](https://mvnrepository.com/artifact/com.jcraft/jsch) for SSH support.
It uses your local private key to access your GIT server.
Unfortunately this file format must be in the _classic_ OpenSSH format because JSch does not support the _new_ OpenSSH format.
If you'll get the error message like

    invalid privatekey: [B@59c40796.

it is likely that your private keyfile is in the wrong format.
You can transform it using `ssh-keygen`:

    ssh-keygen -p -f .ssh/id_rsa -m pem

For more info see this [stackoverflow article](https://stackoverflow.com/questions/53134212/invalid-privatekey-when-using-jsch).
