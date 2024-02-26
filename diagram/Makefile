.PHONY: clean all
PLANTUML=plantuml

all: domain-model.png

domain-model.png: domain-model.txt
	$(PLANTUML) domain-model.txt

clean:
	rm *.png
