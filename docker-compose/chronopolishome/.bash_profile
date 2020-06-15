# .bash_profile

# Get the aliases and functions
if [ -f ~/.bashrc ]; then
        . ~/.bashrc
fi

# User specific environment and startup programs

PATH=$PATH:$HOME/.local/bin:$HOME/bin

export PATH

export BAGIT_HOME=~/bagit
export PATH=$BAGIT_HOME/bin:$PATH
export JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
