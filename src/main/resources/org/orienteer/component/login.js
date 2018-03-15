'use strict';

function configSignInForm(formId) {
    const createChain = (...f) => el => f.forEach(f => f(el));
    const getBtnByType = (type) => $('#' + formId + '>dl>dd>[type=' + type + ']');
    const getBtnContainer = () => $('#' + formId + '>dl>dd:last-child');
    const getLabel = () => $('#' + formId + '>dl>dt');
    const chain = createChain(e => e.addClass('btn btn-primary'),
        e => e.after('<a href="/" class="btn btn-success">Sign Up</a>'));
    chain(getBtnByType('submit'));
    getLabel().hide();
    getBtnByType('reset').hide();
    getBtnContainer().addClass('btn-container');
}