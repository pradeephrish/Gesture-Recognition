function SVDFinder(filein,fileout)
	X = csvread(filein);
    X = transpose(X);
    [U,S,V] = svd(X);
    V
    csvwrite(fileout,V);
end