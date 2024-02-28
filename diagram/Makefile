.PHONY: clean all
PLANTUML=plantuml

all: domain-model.png class-diagram.png

domain-model.png: domain-model.txt
	$(PLANTUML) domain-model.txt

class-diagram.png: class-diagram.txt
	$(PLANTUML) class-diagram.txt

clean:
	rm *.png
