#!/bin/sh

modules=( \ 
	enkan-component-HikariCP \
	enkan-component-doma2 \
	enkan-component-flyway \
	enkan-component-freemarker \
	enkan-component-jackson \
	enkan-component-jetty \
	enkan-component-metrics \
	enkan-component-thymeleaf \
	enkan-component-undertow \
	enkan-core \
	enkan-devel \
	enkan-repl-pseudo \
	enkan-servlet \
	enkan-system \
	enkan-web \
	kotowari \
	kotowari-scaffold \
)

for m in ${modules[@]}
do
  cd $m
  mvn -Psonatype-oss-release deploy
  cd ..
done
