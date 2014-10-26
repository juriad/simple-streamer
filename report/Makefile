FIGURES = 

all: report.pdf

report.pdf: report.tex references.bib $(FIGURES)
	pdflatex report.tex
	bibtex report.aux
	pdflatex report.tex
	pdflatex report.tex

clean:
	rm -f *.aux *.out *.log *.bbl  *.blg  *.dvi *.toc
