function PCAFinder(filein,fileout)
	filein
	X = csvread(filein);
    X
    X = transpose(X);
    X
    [pc,score,latent] = princomp(X);
    pc
    pc=transpose(pc);
    csvwrite(fileout,pc);
end