from stallion import stallion_context

def example_processor():
    return {
        'example_request_path': stallion_context.request
    }
