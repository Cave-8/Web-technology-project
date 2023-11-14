let chosenRow;

function dragit(e) {
	chosenRow = e.target;
}

function dragover(e) {
	let children = Array.from(e.target.parentNode.parentNode.children);
	if (children.indexOf(e.target.parentNode) > children.indexOf(chosenRow))
		e.target.parentNode.after(chosenRow);
	else 
		e.target.parentNode.before(chosenRow);
}
