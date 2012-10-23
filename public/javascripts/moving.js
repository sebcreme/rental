$(document).ready(function(){
	Moving.init()
	
})
Moving = {
	init : function(){
	    $('#rental-detail input').focus(function(){
	        $('button').show()
	    })
	},
	
	ban : function(id, unban){
	    $('#ban').replaceWith('<span id="ban">En cours...</span>')
		$.post((unban ? '/unban/': '/banned/')+id, function(){
		    $('#ban').replaceWith(unban ? 'suivie !' : 'rejeté !')
		    $('#sban').replaceWith('')
            
		    $('#banned').text(parseInt($('#banned').text(), 10) + (unban ? -1 : 1))
    		$('#live').text(parseInt($('#live').text(), 10) + (unban ? 1 : -1))
		})
		
	},
	unban : function(id){
	    this.ban(id, true)
	},
	addComment: function(id){
	    var note = $('#rental-detail input[type=text]').val()
	    $.post('/note', {rentalId: id, note : note}, function(){
	        $('#rental-detail > p:last').after('<p class="comment">'+note+' <span class="date">Now</span></p>')
	    })
	    return false;
	}
}