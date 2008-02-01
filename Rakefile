require 'rake'

desc "Create a GNU-style ChangeLog via svn2cl"
task :ChangeLog do
  system("svn2cl --authors=svn2cl_usermap svn://rubyforge.org/var/svn/debug-commons/intermediate-java/trunk")
end

