CLANG_FORMAT=node_modules/clang-format/bin/linux_x64/clang-format --style=Google
CSS_VALIDATOR=node_modules/css-validator/bin/css-validator
ESLINT=node_modules/eslint/bin/eslint.js
HTML_VALIDATE=node_modules/html-validate/bin/html-validate.js
PRETTIER=node_modules/prettier/bin-prettier.js

node_modules:
	npm install clang-format prettier css-validator html-validate eslint eslint-config-google

pretty: node_modules
	$(PRETTIER) --write src/**/*.{html,css}
	find src/ -iname *.java | xargs $(CLANG_FORMAT) -i
	find src/ -iname *.js | xargs $(CLANG_FORMAT) -i

validate: node_modules
	$(HTML_VALIDATE) src/main/webapp/*.html
	$(CSS_VALIDATOR) src/main/webapp/*.css
	$(ESLINT) src/**/*.js

package:
	mvn package

run: pretty
	mvn package appengine:run

deploy: pretty validate
	mvn package appengine:deploy
