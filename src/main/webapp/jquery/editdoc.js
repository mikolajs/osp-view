

var Document =  dejavu.Class.declare({
	
	initialize : function(){
		$('#docEdit').html($('#docContent').val());
		CKEDITOR.disableAutoInline = true;

		//document.getElementById('docEdit').innerHTML = $('#extraText').val();
		this.editor = CKEDITOR.inline( 'docEdit', {
			extraPlugins: 'sourcedialog,addImage,syntaxhighlight,formula,symbol,image',
			format_tags : 'p;h2;h3;h4;h5;h6;pre;address',
			allowedContent : true,
			disableNativeSpellChecker : false,
			language : 'pl',
			toolbar: [
			        [ 'Sourcedialog' ],
			  		[ 'Cut', 'Copy','Paste', 'PasteText', 'PasteFromWord', '-','Undo', 'Redo' ],
			  		[ 'AddImage', 'Table','Syntaxhighlight','Formula', 'Symbol', "Image" ],
			  		[ 'Link', 'Unlink',	'Anchor' ],
			  		[ 'Find', 'Replace','-', 'SelectAll' ],
			  		'/',
			  		[ 'Bold', 'Italic','Underline', 'Strike','Subscript',	'Superscript', '-','RemoveFormat' ],
			  		[ 'NumberedList',	'BulletedList', '-','Outdent', 'Indent','-', 'Blockquote', '-','JustifyLeft',
						'JustifyCenter','JustifyRight',	'JustifyBlock' ],	
						[ 'Styles', 'Format',	'Font', 'FontSize' ]
						
			  	]
		});
	},
	deleteDoc: function() 
	{
		return confirm("Jesteś pewien, że chcesz skasować cały dokument?");
	},
    saveDoc : function() {
    	$('#docContent').val($('#docEdit').html());
    	return true;
    }
	
});