import {css, html, LitElement} from 'lit';
import {JsonRpc} from 'jsonrpc';
import '@vaadin/text-area';
import '@vaadin/dialog';
import '@vaadin/progress-bar';
import MarkdownIt from 'markdown-it';
import {unsafeHTML} from 'lit/directives/unsafe-html.js';
import {dialogRenderer} from '@vaadin/dialog/lit.js';
import {msg, updateWhenLocaleChanges} from 'localization';

/**
 * This component shows the scheduled methods.
 */
export class SolrQueryBuilder extends LitElement {
    static styles = css`
        :host {
            display: flex;
            flex-direction: column;
            gap: 10px;
            height: 100%;
            padding-left: 5px;
            padding-right: 5px;
        }
    
        .input {
            display: flex;
            gap: 5px;
            align-items: baseline;
            justify-content: space-between;
        }
        vaadin-text-area {
            width: 100%;
        }
        
    `;

    //TODO: Properties
    static properties = {
        _description: {state: true},
        _isLoading: {state: true},
        _query: {state: true},
        _example: {state: true}
    };
    jsonRpc = new JsonRpc(this);

    constructor() {
        super();
        updateWhenLocaleChanges(this);
        //TODO: Properties
        this._isLoading = false;
        this._query = null;
        this._example = null;
        this._description = '';
        this.md = new MarkdownIt();
    }

    render() {
        return html`
            ${this._renderLoadingDialog()}
            ${this._renderInput()}
            ${this._renderOutput()}
        `;
    }

    _renderLoadingDialog() {
        return html`
            <vaadin-dialog
                    .opened="${this._isLoading}"
                    @closed="${() => {
                        this._isLoading = false;
                    }}"
                    ${dialogRenderer(
                            () => html`
                                <label class="text-secondary" id="pblabel">
                                    ${msg(
                                            'Talking to the Dev Assistant ... please wait',
                                            {id: 'quarkus-solr-talking-to-dev-assistant'}
                                    )}
                                </label>
                                <vaadin-progress-bar indeterminate aria-labelledby="pblabel">
                                </vaadin-progress-bar>
                            `,
                            []
                    )}
            ></vaadin-dialog>
        `;
    }

    _renderInput() {
        return html`
            <div class="input">
                <vaadin-text-area
                        min-rows="4"
                        max-rows="8"
                        .label=${msg(
                                'Describe the query you want to create',
                                {id: 'quarkus-solr-describe-query'}
                        )}
                        .value=${this._description ?? ''}
                        @value-changed=${(e) => {
                            this._description = e.detail.value;
                        }}>
                </vaadin-text-area>
                ${this._renderButton()}
            </div>
        `;
    }

    _renderOutput() {
        if (this._query) {
            const htmlContent = this.md.render(this._example);
            return html`
                <div class="output">
                    <h3>
                        ${msg('Query:', {id: 'quarkus-solr-query-label'})}
                    </h3>
                    <code>${this._query}</code>
                    <h3>
                        ${msg('Example', {id: 'quarkus-solr-example'})}
                    </h3>
                    <div class="example">
                        ${unsafeHTML(htmlContent)}
                    </div>
                </div>
            `;
        }
    }

    _renderButton() {
        if (!this._isLoading) {
            return html`
                <vaadin-button @click="${this._createQuery}">
                    ${msg('Create Query', {id: 'quarkus-solr-create-query'})}
                </vaadin-button>
            `;
        }
    }

    _createQuery() {
        this._isLoading = true;
        document.body.style.cursor = 'wait';

        this.jsonRpc.createQuery({description: this._description}).then(jsonResponse => {
            this._isLoading = false;
            document.body.style.cursor = 'default';
            console.log(jsonResponse);
            this._query = jsonResponse.result.query;
            this._example = jsonResponse.result.example;
        }).catch(error => {
            console.log(error)
        });
    }
}

customElements.define('qwc-quarkus-solr-query-builder', SolrQueryBuilder);
