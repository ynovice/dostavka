import withHeaderAndFooter from "../hoc/withHeaderAndFooter";
import {useCallback, useEffect, useState} from "react";
import requiresUser from "../hoc/requiresUser";
import "../css/CartPage.css";
import Api from "../Api";

function CartPage() {

    const CartState = {
        LOADING: "LOADING",
        LOADED: "LOADED"
    }

    const [cart, setCart] = useState(null);
    const [cartState, setCartState] = useState(CartState.LOADING);

    const reloadCart = useCallback((abortSignal) => {

        Api.getCart(abortSignal)
            .then(retrievedCart => {
                setCart(retrievedCart);
                setCartState(CartState.LOADED);
            });
    }, [CartState.LOADED]);

    useEffect(() => {

        const abortController = new AbortController();

        reloadCart(abortController.signal);

        return () => abortController.abort();
        }, [reloadCart]);

    const handleDecrementItemQuantityInCartClick = (item) => {
        Api.decrementItemQuantityInCart(item["id"])
            .then(() => reloadCart(new AbortController().signal));
    }

    const handleIncrementItemQuantityInCartClick = (item, cartEntry) => {

        if(item["quantity"] < cartEntry["quantity"] + 1) {
            return;
        }

        Api.incrementItemQuantityInCart(item["id"])
            .then(() => reloadCart(new AbortController().signal));
    }

    const handleRemoveItemFromCartClick = (item) => {
        Api.removeItemFromCart(item["id"])
            .then(() => reloadCart(new AbortController().signal));
    }
    const [similarItemsPage, setSimilarItemsPage] = useState(null);
    const [cachedSimilarItemsPage, setCachedSimilarItemsPage] = useState(null);

    useEffect(() => {

        const abortController = new AbortController();

        Api.getItemsPageByFilterParams({}, abortController.signal)
            .then(retrievedSimilarItemsPage => setCachedSimilarItemsPage(retrievedSimilarItemsPage));

        return () => abortController.abort();

    }, []);

    useEffect(() => {

        const abortController = new AbortController();

        if(cartState === CartState.LOADING) {
            return;
        }

        const itemsInCartIds = [];
        cart["entries"].forEach(cartEntry => {
            itemsInCartIds.push(cartEntry["item"]["id"]);
        });

        const actualSimilarItems = [];

        if(!cachedSimilarItemsPage) {
            return;
        }

        for (let i = 0; i < cachedSimilarItemsPage["items"].length; i++) {
            if(itemsInCartIds.indexOf(cachedSimilarItemsPage["items"][i]["id"]) === -1) {
                actualSimilarItems.push(cachedSimilarItemsPage["items"][i]);
            }
        }
        cachedSimilarItemsPage["items"] = actualSimilarItems;

        setSimilarItemsPage(cachedSimilarItemsPage);

        return () => abortController.abort();
    }, [CartState.LOADING, cachedSimilarItemsPage, cart, cartState]);

    return (
        <div className="CartPage">

            <div className="left-side">

                <div className="page-title">Корзина</div>

                {cartState === CartState.LOADED &&
                    <div className="cart-items">

                        {cart["entries"].length === 0 &&
                            <div className="no-items-message-container">
                                <p>Ваша корзина пуста.</p>
                                <a href="/catalog" className="link">За покупками →</a>
                            </div>
                        }

                        {cart["entries"].map(cartEntry => {

                            const item = cartEntry["item"];

                            const imageUrl = item["images"].length > 0 ?
                                Api.getImageUrlByImageId(item["images"][0]["id"]) :
                                "/ui/item-placeholder.png";

                            const noMoreToAdd = item["quantity"] <= cartEntry["quantity"];

                            return (
                                <div key={"cart-entry-" + cartEntry["id"]} className="cart-item">

                                    <div className="image-container">
                                        <img src={imageUrl} alt={item["name"]}/>
                                    </div>

                                    <div className="cart-item-info">

                                        <div className="cart-item-name">{item["name"]}</div>
                                        <div className="cart-item-total-price">
                                            {item["price"] * cartEntry["quantity"]}₽
                                        </div>

                                        <div className="cart-item-controls">
                                            <div className="left">
                                                <span className="link danger"
                                                      onClick={() => handleRemoveItemFromCartClick(item)}>
                                                    Убрать
                                                </span>
                                            </div>
                                            <div className="right">

                                                {noMoreToAdd && <p>Больше не добавить</p>}

                                                <div className="counter-controls">
                                                        <span className="noselect"
                                                              onClick={() => handleDecrementItemQuantityInCartClick(item)}>-</span>
                                                    <span className="noselect disabled">{cartEntry["quantity"]}</span>
                                                    <span className={"noselect" + (noMoreToAdd ? " disabled" : "")}
                                                          onClick={() => handleIncrementItemQuantityInCartClick(item)}>
                                                            +
                                                        </span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            );
                        })}

                    </div>
                }

                {cartState === CartState.LOADED &&
                    <div className="cart-summary">
                        <div className="items-total-count">Всего товаров: {cart["totalItems"]}</div>
                        <div className="items-total-price">
                            <p>Итого</p>
                            <p>{cart["totalPrice"]}₽</p>
                        </div>
                        {cart["totalItems"] > 0 &&
                            <div className="button-container">
                                <input onClick={() => window.location.href = "/confirm-reserve"}
                                       type="button"
                                       className="button"
                                       value="Зарезервировать"/>
                            </div>
                        }
                    </div>
                }
            </div>

            <div className="right-side">
                {similarItemsPage && similarItemsPage["items"] && similarItemsPage["items"].length > 0 &&
                    <div className="similar-items">
                        <div className="section-title">А ещё тебе точно нужно это:</div>

                        <div className="similar-items-catalog">
                            {similarItemsPage["items"].map(similarItem => {

                                const imageUrl = similarItem["images"].length > 0 ?
                                    Api.getImageUrlByImageId(similarItem["images"][0]["id"]) :
                                    "/ui/item-placeholder.png";

                                return (
                                    <div key={similarItem["id"]} className="item">
                                        <a href={"/item/" + similarItem["id"]}>
                                            <div className="image-container">
                                                <img src={imageUrl} alt={similarItem["name"]}/>
                                            </div>
                                            <p className="item-name">{similarItem["name"]}</p>
                                            <p className="item-price">{similarItem["price"]}₽</p>
                                        </a>
                                    </div>
                                );
                            })}
                        </div>
                    </div>
                }

            </div>

        </div>
    );
}

export default withHeaderAndFooter(requiresUser(
    CartPage,
    "Войдите в аккаунт, чтобы пользоваться корзиной."
));
